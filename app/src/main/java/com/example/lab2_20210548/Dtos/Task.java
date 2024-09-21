package com.example.lab2_20210548.Dtos;

import java.io.Serializable;

public class Task implements Serializable {
    private Tareas[] todos;
    private Integer total;
    private Integer skip;
    private Integer limit;

    public Tareas[] getTodos() {
        return todos;
    }

    public void setTodos(Tareas[] todos) {
        this.todos = todos;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
