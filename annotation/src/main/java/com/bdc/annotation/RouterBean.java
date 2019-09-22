package com.bdc.annotation;

import javax.lang.model.element.Element;

public class RouterBean {

    public enum Type{
        ACITIVTY
    }
    private Type type;
    private Element element;
    private Class clazz;
    private String group;
    private String path;

    public static RouterBean create(Type type, Class clazz, String path, String group){
        return new RouterBean(type,clazz,path,group);
    }

    private RouterBean(Type type, Class clazz, String path, String group) {
        this.type = type;
        this.clazz = clazz;
        this.group = group;
        this.path = path;
    }

    private RouterBean(Builder builder){
        this.path=builder.path;
        this.group=builder.group;
        this.element=builder.element;

    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public static final class Builder{
        private Element element;
        private String group;
        private String path;

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public RouterBean build(){
            if(path==null || path.length()==0){
                throw new IllegalArgumentException("path cannot be empty");
            }
            return new RouterBean(this);
        }
    }


    @Override
    public String toString() {
        return "RouterBean{" +
                "group='" + group + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
