package org.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XiaomiFollowCommentDataEntity {

    private String AuthorId;

    private String AuthorName;

    private int AuthorLevel;

    private String AuthorTitle;

    private String CommentId;

    /**
     * 博客PostId
     */
    private String SubjectId;

    private String SourceId;

    private String sourceUserId;

    private String IpRegion;

    private String CommentText;

    private int SupportNum;

    private String PublishDate;

    private String CollectTime;

}
