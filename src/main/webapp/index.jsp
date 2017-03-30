<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="cn.maluit.WeChat.Common.AccessTokenInfo" %>
<html>
<head>
    <title>Wechat</title>
</head>
<body>
微信接口测试
<hr/>
access_token为：<%=AccessTokenInfo.accessToken.getAccessToken()%>
<br/>
expires_in:<%=AccessTokenInfo.accessToken.getExpiresin()%>
</body>
</html>