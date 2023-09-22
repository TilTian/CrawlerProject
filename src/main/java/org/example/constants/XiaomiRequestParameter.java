package org.example.constants;

public class XiaomiRequestParameter {

    /**
     * 小米社区帖子请求地址为  前参 + after + 后参
     */
    public final static String MIUI_FRONT_PARA = "https://api.vip.miui.com/api/community/board/search/announce" +
            "?ref=&pathname=%2Fmio%2FsingleBoard&version=dev.230112&oaid=false&device=&restrict_imei=" +
            "&miui_big_version=&model=&miuiBigVersion=&boardId=558495&limit=10&after=";
    public final static String MIUI_BACK_PARA = "&profileType=1&displayName=%E5%85%A8%E9%83%A8&filterTab=1";

    /**
     * 小米社区主贴评论请求地址为 + postId + after（起始index） + 后参     (+ fromBoardId)
     */
    public final static String COMMENT_FONT_PARA = "https://api.vip.miui.com/mtop/planet/vip/content/comments" +
            "?ref=&pathname=%2Fmio%2Fdetail&version=dev.230112&oaid=false&device=&restrict_imei=&miui_big_version=&model=&miuiBigVersion=&limit=10";
    public final static String COMMENT_BACK_PARA = "&sortType=1";
}
