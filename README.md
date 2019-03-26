# SnapShare
SnapShare is a application that allow you to share your videoes, images and all kinds of files to your frind, by uploading them to a FTP server. 

## Configuration
Before installing this app, make sure that you have set up a FTP server with port 21, and set the host, username, password in `MainActivity.java`

	private final String host = "192.168.129.1";
	private final String userName = "FtpUser";
	private final String passWord = "112233";

## Features
### Server interface
You can download, rename and delete files, with multiselect.       
[Server interface]![1]
### Gallery interface
You can choose images and videoes from all of your media files, or use your camera to take on at once. Moreover you can do some simple crop or drawing on your image.       
[Gallery interface]![2]
### Local interface
Where you can upload, rename and delete local files,  with multiselect.       
[Local interface]![3]
### Auto monitor Internet condition
This app can auto-reconnect to the server after your internet connection is failed.       
[1]:Server.png
[2]:Gallery.png
[3]:Local.png
