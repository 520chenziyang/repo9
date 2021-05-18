package com.renxingbao.spring_fivth.util;

public class Award {
    /**编号*/
    public String id;

    /**数量（该类奖品数量）*/
    public int count;

    /**价值（该类奖品价值积分）*/
    public int pointVal;

    /**剩余数量（该类奖品剩余数量）*/
    public int remainCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPointVal() {
        return pointVal;
    }

    public void setPointVal(int pointVal) {
        this.pointVal = pointVal;
    }

    public int getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
    }

    /**
     *
     * @param id *编号*
     * @param count 数量（该类奖品数量）*
     * @param pointVal 价值（该类奖品价值积分）*
     * @param remainCount 剩余数量（该类奖品剩余数量）
     */
    public Award( String id, int count, int pointVal, int remainCount) {
        this.id = id;
        this.count = count;
        this.pointVal = pointVal;
        this.remainCount = remainCount;
    }

    @Override
    public String toString() {
        return "Award [id=" + id + ", count=" + count + ", pointVal="
                + pointVal + ", remainCount=" + remainCount + "]";
    }

}
