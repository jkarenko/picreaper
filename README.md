picreaper
=========
Takes images, resizes them and then crops them by hacking off pieces with the least entropy
until desired aspect ratio is reached.

usage
-----
`$ groovy picreaper [imagename]`


example result
--------------
![original image](tall.jpg "original image")
![cropped image](160_tall.jpg "cropped image")

![original image](wide.jpg "original image")
![cropped image](160_wide.jpg "cropped image")


features
--------

resizes and crops images to hardcoded format


future features
---------------

- command line arguments
- option to use as a class
