package com.justforfun.phoneverification;

public class WebItems {

    private String Header;
    private String url;
    private String Description;
    private boolean isVideo;

    public WebItems(){

    }

    public WebItems(String header,String url, String des, boolean isVideo){
        this.Header = header;
        this.url = url;
        this.Description = des;
        this.isVideo = isVideo;
    }

    public void setHeader(String header) {this.Header = header;}

    public String getHeader() {return Header;}

    public void setUrl(String url) {this.url = url;}

    public String getUrl() {return url;}

    public void setDescription(String description) {this.Description = description;}

    public String getDescription() {return Description;}

    public void setIsVideo(boolean isVideo) {this.isVideo = isVideo;}

    public boolean getIsVideo() {return isVideo;}
}
