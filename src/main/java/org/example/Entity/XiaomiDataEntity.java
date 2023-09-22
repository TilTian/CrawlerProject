package org.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XiaomiDataEntity {

    private String UserId;

    private String UserName;

    private int UserLevel;

    private String UserTitle;

    private String TextTitle;

    private String Summary;

    private String BoardId;

    private String BoardName;

    private String PostId;

    private String Url;

    private String IpRegion;

    private int LikeCnt;

    private int CommentCnt;

    private String PublishDate;

    private String CollectTime;
}
