package org.example.constants;

public class DataFormatConstants {
    public final static String JSON_FORMAT = "{\"include\": \"data[*].is_normal,admin_closed_comment,reward_info,is_collapsed,annotation_action,annotation_detail,collapse_reason,is_sticky,collapsed_by,suggest_edit,comment_count,can_comment,content,editable_content,voteup_count,reshipment_settings,comment_permission,created_time,updated_time,review_info,relevant_info,question,excerpt,relationship.is_authorized,is_author,voting,is_thanked,is_nothelp,is_labeled,is_recognized;data[*].mark_infos[*].url;data[*].author.follower_count,badge[*].topics\",\n" +
            "\"limit\": 20,\n" + //每列显示的个数
            "\"offset\": 15,\n" + //页数
            "\"platform\": \"desktop\",\n" +
            "\"sort_by\": \"default\"}";
    public final static int LIMIT_Text_Content_LENTH = 120;
}
