package com.phc.hcmap.pojo;

/**
 * 版权：没有版权 看得上就用
 *
 * @author peng
 * 创建日期：2021/1/28 15
 * 描述：
 */
public class LatLngPojo {

    /**
     * area :
     * city : 市辖区
     * country : 中国
     * lat : 39.910924547299565
     * lng : 116.4133836971231
     * province : 北京市
     */

    private String area;
    private String city;
    private String country;
    private String lat;
    private String lng;
    private String province;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
