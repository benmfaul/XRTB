#!/bin/sh
scp -i ~ben/certs/rtb4free_key.pem index.html ubuntu@rtb4free.com:/usr/share/nginx/www/index.html
scp -i ~ben/certs/rtb4free_key.pem index.html ubuntu@rtb4free.com:/usr/share/nginx/www/index.html
scp -i ~ben/certs/rtb4free_key.pem blog_link.html ubuntu@rtb4free.com:/usr/share/nginx/www/blog_link.html
scp -i ~ben/certs/rtb4free_key.pem contact.html ubuntu@rtb4free.com:/usr/share/nginx/www/contact.html
scp -i ~ben/certs/rtb4free_key.pem details.html ubuntu@rtb4free.com:/usr/share/nginx/www/details.html
scp -i ~ben/certs/rtb4free_key.pem behavior.html ubuntu@rtb4free.com:/usr/share/nginx/www/behavior.html
scp -i ~ben/certs/rtb4free_key.pem license.html ubuntu@rtb4free.com:/usr/share/nginx/www/license.html
scp -i ~ben/certs/rtb4free_key.pem overview.html ubuntu@rtb4free.com:/usr/share/nginx/www/overview.html
scp -i ~ben/certs/rtb4free_key.pem mxbidders.html ubuntu@rtb4free.com:/usr/share/nginx/www/mxbidders.html
scp -i ~ben/certs/rtb4free_key.pem geocode.html ubuntu@rtb4free.com:/usr/share/nginx/www/geocode.html
scp -i ~ben/certs/rtb4free_key.pem resources.html ubuntu@rtb4free.com:/usr/share/nginx/www/resources.html
scp -i ~ben/certs/rtb4free_key.pem wurfl.html ubuntu@rtb4free.com:/usr/share/nginx/www/wurfl.html
scp -i ~ben/certs/rtb4free_key.pem video.html ubuntu@rtb4free.com:/usr/share/nginx/www/video.html
scp -i ~ben/certs/rtb4free_key.pem ../web/exchange.html ubuntu@rtb4free.com:XRTB/exchange.html
scp -i ~ben/certs/rtb4free_key.pem arch/* ubuntu@rtb4free.com:/usr/share/nginx/www/arch
scp -i ~ben/certs/rtb4free_key.pem -r ../html ubuntu@rtb4free.com:/usr/share/nginx/www
scp -i ~ben/certs/rtb4free_key.pem -r ../javadoc ubuntu@rtb4free.com:/usr/share/nginx/www
