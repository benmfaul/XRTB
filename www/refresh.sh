#!/bin/sh
scp -i ~ben/certs/rtb4free_key.pem $1 ../html ubuntu@rtb4free.com:/usr/share/nginx/www
