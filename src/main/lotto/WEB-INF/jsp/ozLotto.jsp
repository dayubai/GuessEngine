<!DOCTYPE HTML>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
<head>
	<title>My Lotto</title>
	<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
</head>
<body>
	
    <input type="text" name="games" value="18"/>&nbsp;<input type="button"value="New Ticket"/>
    
    <h2>Ticket History</h2>
    <div>
 <c:if test="${not empty tickets}">
        <c:forEach var="ticket" items="${tickets}">
             <span>Ticket: ${ticket.draw}</span>
             <c:forEach var="result" items="${ticket.results}">
             <div>${result.numbers}</div>
             </c:forEach>
        </c:forEach>
    </div>
    </c:if>   
    <script type="text/javascript">
    	
    </script>
</body>
</html>