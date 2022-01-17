<%-- 
    Document   : editItem
    Created on : 8 Jan 2022, 19:59:02
    Author     : ISA06002471
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="header.jsp" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Admin - Edit Item</title>
    </head>
    <body>
        <form action ="./editItem" method="POST">
            <table class="table">
                <thead>

                </thead>
                <tbody>
                    <tr>
                        <td>Item ID</td>
                        <td>${editItem.uuid}</td>
                    </tr>
                    <tr>
                        <td>Name</td>
                        <td><input type="text" required name="updateName" value="${editItem.name}"></td>
                    </tr>
                    <tr>
                        <td>Price</td>
                        <td><input type="number" required name="price" value="${editItem.price}"></td>
                    </tr>
                </tbody>
            </table>
            <input type="hidden" name="currentUuid" value="${editItem.uuid}">
            <input type="hidden" name="currentName" value="${editItem.name}">
            <button class="btn" type="submit">Update</button>
            
            <form action="./viewCatalog" method="POST">
                <input type="hidden" name="action" value="delete"/>
                <input type="hidden" name="name" value="${modifyItem.name}"/>
                <input type="hidden" name="uuid" value="${modifyItem.uuid}"/>
                <button class="btn" type="submit" >Delete</button>
            </form>

        </form>
    </body>
</html>
