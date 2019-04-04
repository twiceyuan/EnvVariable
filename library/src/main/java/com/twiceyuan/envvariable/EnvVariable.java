package com.twiceyuan.envvariable;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.twiceyuan.envvariable.annotation.VariableProp;
import com.twiceyuan.envvariable.model.Variable;
import com.twiceyuan.envvariable.ui.ConfigActivity;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnvVariable {

    private static final String TAG = "EnvVariable";
    private static final String CACHE_PATH = "EnvVariable";

    private static final Map<String, Variable> cache = new ConcurrentHashMap<>();

    private static Class envConfigClass;
    private static Context applicationContext;

    private static Runnable refreshAction = null;

    public static <T> T register(final Context context, Class<T> clazz) {
        envConfigClass = clazz;
        applicationContext = context.getApplicationContext();
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            VariableProp variableAnnotation = method.getAnnotation(VariableProp.class);
            return getEnvVariableByAnnotation(context, variableAnnotation);
        });
    }

    private static Variable getEnvVariableByAnnotation(Context context, VariableProp variableAnnotation) throws Exception {
        String name = variableAnnotation.name();

        // 1. 内存中有优先使用内存的
        Variable cachedVariable = cache.get(name);

        if (cachedVariable != null) {
            return cachedVariable;
        }

        // 2. 判断外存中是否有，有的话取外存并缓存到内存中
        Variable diskCachedVariable = readFromDisk(context, name);
        if (diskCachedVariable != null) {
            cache.put(name, diskCachedVariable);
            return diskCachedVariable;
        }

        // 3. 都没有，则根据注解获取默认的
        Variable newVariable = generateVariableByDefault(variableAnnotation);

        // 缓存到内存中
        cache.put(name, newVariable);

        return newVariable;
    }

    public static List<Variable> listAllVariables() {
        if (envConfigClass == null) {
            throw new IllegalStateException("Call `EnvVariable.register()` first!");
        }
        List<Variable> variables = new ArrayList<>();
        for (Method method : envConfigClass.getDeclaredMethods()) {
            VariableProp annotation = method.getAnnotation(VariableProp.class);
            if (annotation != null) {
                try {
                    variables.add(getEnvVariableByAnnotation(applicationContext, annotation));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        return variables;
    }

    /**
     * 从 Disk 读取变量
     */
    private static Variable readFromDisk(Context context, String name) {
        try {
            File cacheDir = new File(context.getCacheDir(), CACHE_PATH);
            if (!cacheDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
            }
            File cacheFile = new File(cacheDir, name);
            return (Variable) new ObjectInputStream(new FileInputStream(cacheFile)).readObject();
        } catch (IOException e) {
            Log.v(TAG, "暂无持久化的变量[" + name + "]，使用默认值");
            return null;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存一个环境变量到 Disk
     */
    private static void saveToDisk(Context context, Variable variable) {
        try {
            File cacheDir = new File(context.getCacheDir(), CACHE_PATH);
            if (!cacheDir.exists()) {
                boolean mkResult = cacheDir.mkdirs();
                if (!mkResult) {
                    Log.e(TAG, "cacheDir.mkdirs() failed: " + cacheDir.getAbsolutePath());
                }
            }
            File cacheFile = new File(cacheDir, variable.name);
            new ObjectOutputStream(new FileOutputStream(cacheFile)).writeObject(variable);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * 同步内存中的额缓存到 Disk
     */
    public static void syncCache() {
        for (Variable value : cache.values()) {
            saveToDisk(applicationContext, value);
        }
    }

    /**
     * 生成默认环境变量
     */
    private static Variable generateVariableByDefault(VariableProp variable) throws Exception {
        Variable newVariable = new Variable();

        newVariable.name = variable.name();
        newVariable.desc = variable.desc();

        Class<? extends Variable.DefaultItemProvider> defaultProviderClass = variable.defaultValue();
        Class<? extends Variable.Item> defaultVariableClass = getDefaultVariableClass(defaultProviderClass);

        newVariable.currentValue = getValueByConstant(defaultVariableClass);

        Class<? extends Variable.Item>[] selectionClasses = variable.selections();
        newVariable.selections = new ArrayList<>();
        for (Class<? extends Variable.Item> selectionClass : selectionClasses) {
            newVariable.selections.add(getValueByConstant(selectionClass));
        }
        return newVariable;
    }

    /**
     * 根据默认值提供者获取默认值
     */
    private static Class<? extends Variable.Item> getDefaultVariableClass(
            Class<? extends Variable.DefaultItemProvider> providerClass
    ) throws Exception {
        Variable.DefaultItemProvider provider = providerClass.newInstance();
        return provider.provide();
    }

    /**
     * 获取常量值
     */
    private static Variable.Item getValueByConstant(
            Class<? extends Variable.Item> constantClass
    ) throws Exception {
        Variable.Item instance = constantClass.newInstance();
        final String name = instance.name;
        final String value = instance.value;
        final boolean isEditable = instance.isEditable;
        return new Variable.Item(name, value, isEditable);
    }

    /**
     * 注册刷新动作
     *
     * @param refreshAction 需要应用变量到运行时的附加动作
     */
    public static void registerRefreshAction(Runnable refreshAction) {
        EnvVariable.refreshAction = refreshAction;
    }

    public static void saveWithRefresh() {
        syncCache();

        if (EnvVariable.refreshAction != null) {
            EnvVariable.refreshAction.run();
        }
    }

    public static void openConfig(Context context) {
        Intent intent = new Intent(context, ConfigActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
