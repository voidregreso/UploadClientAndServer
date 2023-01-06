<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>UploadServlet Test Page</title>
</head>
<body>
    <h2>Esta página se utiliza para simular que un cliente remoto envía una petición POST al UploadServlet y transfiere el fichero. Siéntete libre de utilizarlo.</h2>
    <form action="upload_file" method="post" enctype="multipart/form-data">
    Upload files: <input type="file" name="file" />
    <p /><input type="submit" value="Upload" style="width: 70px" />
</form>
</body>
</html>