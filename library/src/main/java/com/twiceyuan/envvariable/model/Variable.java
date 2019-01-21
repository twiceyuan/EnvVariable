package com.twiceyuan.envvariable.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Variable implements Serializable {

    public String name;             // 名称
    public String desc;             // 说明
    public Item currentValue;       // 默认值/当前选项
    public ArrayList<Item> selections;   // 可选项

    public String getValue() {
        return currentValue.value;
    }

    public static class Item implements Serializable {

        public final String name;
        public final boolean isEditable;
        public String value;

        public Item(String name, String value, boolean isEditable) {
            this.name = name;
            this.value = value;
            this.isEditable = isEditable;
        }

        public Item(String name, String value) {
            this.name = name;
            this.value = value;
            isEditable = false;
        }

        public void updateValue(String value) {
            this.value = value;
        }
    }

    public interface DefaultItemProvider {
        Class<? extends Item> provide();
    }
}
