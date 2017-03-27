<!DOCTYPE HTML>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
<head>
	<title>My OZ Lotto</title>
	<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
</head>
<body>
    <a href="/ozLotto/rfpredict">Back</a>
    <h2>Draw ${prediction.drawNumber}</h2>
    
    <table>
			<c:forEach var="result" items="${prediction.predictionObjects}">
				<tr>
					<td>${result.number}</td>
					<td>${result.probability}</td>
					<td>${result.prediction}</td>
				</tr>
			</c:forEach>
	</table>
    
</body>
</html>