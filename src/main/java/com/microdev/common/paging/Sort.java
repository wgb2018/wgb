package com.microdev.common.paging;

/**
 * @author liutf
 */
public class Sort {
    private Direction direction;
    private String field;

    public Sort(Direction direction, String field) {
        this.direction = direction;
        this.field = field;
    }

    public Sort() {
    }


    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
