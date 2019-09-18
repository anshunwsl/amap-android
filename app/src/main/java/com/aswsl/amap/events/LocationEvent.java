package com.aswsl.amap.events;

import com.amap.api.location.AMapLocation;

/**
 * Created by agisv2 on 2019-08-03 14:06
 *
 * @author agisv2 wangsl@dse.cn
 * @version 1.0.0
 */
public class LocationEvent {
    //
    private AMapLocation mapLocation=null;
    //
    private  long pointCount=0L;
    //

    public long getPointCount() {
        return pointCount;
    }

    public void setPointCount(long pointCount) {
        this.pointCount = pointCount;
    }

    //
    public AMapLocation getMapLocation() {
        return mapLocation;
    }
    public void setMapLocation(AMapLocation mapLocation) {
        this.mapLocation = mapLocation;
    }
}
