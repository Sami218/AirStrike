FROM nginx:alpine
COPY src/main/webapp /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE ${PORT}
CMD sh -c "envsubst '\$PORT' < /etc/nginx/nginx.conf > /tmp/nginx.conf && mv /tmp/nginx.conf /etc/nginx/nginx.conf && nginx -g 'daemon off;'"
