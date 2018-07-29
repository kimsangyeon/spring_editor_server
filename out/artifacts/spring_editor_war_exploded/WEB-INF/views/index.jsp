<%--
  Created by IntelliJ IDEA.
  User: kimsangyeon
  Date: 2018. 7. 28.
  Time: PM 1:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
  <head>
    <script type="text/javascript" src="<spring:url value="resources/js/synapeditor.js"/>"></script>
  </head>
  <body>
  Hello World
  <div id="synapEditor" class="container" style="max-width:800px"></div>
  <script>
    window.editor = new SynapEditor('synapEditor');
  </script>
  </body>
</html>
