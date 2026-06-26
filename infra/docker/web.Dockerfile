FROM nginx:1.27-alpine

COPY infra/docker/web.nginx.conf /etc/nginx/conf.d/default.conf
COPY apps/web/dist /usr/share/nginx/html

EXPOSE 80