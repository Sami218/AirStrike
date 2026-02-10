FROM nginx:alpine
COPY src/main/webapp /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template
