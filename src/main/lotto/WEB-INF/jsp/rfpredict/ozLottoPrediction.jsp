<!DOCTYPE HTML>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
<head>
	<title>My Lotto</title>
	<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
</head>
<body>
    
	<form action="/ozLotto/rfpredict/run" method="post">
    <input type="text" name="draw" value=""/>&nbsp;<input type="submit"value="Forest Random Prediction"/>
    </form>
    
    <h2>OZ Forest Random Predictions</h2>
    
    <table>
		<c:if test="${not empty predictions}">
			<c:forEach var="p" items="${predictions}">
				<tr>
					<td>Draw: <a href="/ozLotto/rfpredict/draw/${p.drawNumber}">${p.drawNumber}</a></td>
				</tr>
			</c:forEach>
		</c:if>
	</table>
    
    <script type="text/javascript">
    	
    </script>
</body>
</html>