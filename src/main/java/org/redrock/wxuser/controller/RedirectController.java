package org.redrock.wxuser.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.redrock.wxuser.util.HttpsUtil;
import org.redrock.wxuser.util.UserInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String WX_APPID = "wx3faab44f6c3df1c0";
    public static final String WX_APPSECRET = "93a1458ff94cfe569f5ef66b2c7e4898";

    /**
     *
     /**
     * 微信网页授权流程:
     * 1. 用户同意授权,获取 code
     * 2. 通过 code 换取网页授权 access_token
     * 3. 使用获取到的 access_token 和 openid 拉取用户信息
     * @param code  用户同意授权后,获取到的code
     * @param state 重定向状态参数
     * @return
     */

    @GetMapping("/getinfo")
    public String wechatLogin(@RequestParam(name = "code", required = false) String code,
                              @RequestParam(name = "state") String state) {

        logger.info("收到微信重定向跳转.");
        logger.info("用户同意授权,获取code:{} , state:{}", code, state);

        if (code != null || !(code.equals(""))) {

            String WebAccessToken = "";
            String openId = "";
            String nickName,sex,openid = "";
            String REDIRECT_URI = "mis.cqupt.deu.cn/getinfo";
            String SCOPE = "snsapi_userinfo";

            String getCodeUrl = UserInfoUtil.getCode(WX_APPID, REDIRECT_URI, SCOPE);
            logger.info("用户授权, get Code URL:{}", getCodeUrl);

            String tokenUrl = UserInfoUtil.getWebAccess(WX_APPID, WX_APPSECRET, code);
            logger.info("get Access Token URL:{}", tokenUrl);

            String response = HttpsUtil.httpsRequestToString(tokenUrl, "GET", null);

            JSONObject jsonObject = JSON.parseObject(response);
            logger.info("请求到的Access Token:{}", jsonObject.toJSONString());


            if (null != jsonObject) {
                try {

                    WebAccessToken = jsonObject.getString("access_token");
                    openId = jsonObject.getString("openid");
                    logger.info("获取access_token成功!");
                    logger.info("WebAccessToken:{} , openId:{}", WebAccessToken, openId);

                    String userMessageUrl = UserInfoUtil.getUserMessage(WebAccessToken, openId);
                    logger.info("获取用户信息的URL:{}", userMessageUrl);

                    String userMessageResponse = HttpsUtil.httpsRequestToString(userMessageUrl, "GET", null);
                    JSONObject userMessageJsonObject = JSON.parseObject(userMessageResponse);
                    logger.info("用户信息:{}", userMessageJsonObject.toJSONString());

                    if (userMessageJsonObject != null) {
                        try {
                            nickName = userMessageJsonObject.getString("nickname");
                            sex = userMessageJsonObject.getString("sex");
                            sex = (sex.equals("1")) ? "男" : "女";
                            openid = userMessageJsonObject.getString("openid");

                            logger.info("用户昵称:{}", nickName);
                            logger.info("用户性别:{}", sex);
                            logger.info("OpenId:{}", openid);
                        } catch (JSONException e) {
                            logger.error("获取用户信息失败");
                        }
                    }
                } catch (JSONException e) {
                    logger.error("获取Web Access Token失败");
                }
            }
        }
        return "成功";
    }
}