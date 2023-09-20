package org.example.Entity;

import lombok.Data;

@Data
public class XiaomiDataEntity {

    private String UserId;

    private String UserName;

    private int Level;

    private String Title;

    private String Summary;

    private String BoardId;

    private String BoardName;

    private String Url;

    private String IpRegion;

    private int LikeCnt;

    private int CommentCnt;
}
