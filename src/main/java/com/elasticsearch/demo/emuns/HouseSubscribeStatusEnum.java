package com.elasticsearch.demo.emuns;


/**
 * 预约状态码
 * @author zhumingli
 */
public enum HouseSubscribeStatusEnum {
    /**
     *  未预约
     */
    NO_SUBSCRIBE(0),
    /**
     *  已加入待看清单
     */
    IN_ORDER_LIST(1),
    /**
     *  已经预约看房时间
     */
    IN_ORDER_TIME(2),
    /**
     *  已完成预约
     */
    FINISH(3);

    private int value;

    HouseSubscribeStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HouseSubscribeStatusEnum of(int value) {
        for (HouseSubscribeStatusEnum status : HouseSubscribeStatusEnum.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return HouseSubscribeStatusEnum.NO_SUBSCRIBE;
    }
}
