# STAGE 1: Define source of Dynatrace files

FROM fcr.ford.com/eop_dynatrace/oneagent-codemodules/nginx:315 AS dynatrace_source


# STAGE 2: Update base nginx image and assemble application files
# For more information see
# https://catalog.redhat.com/en/software/containers/ubi10/nginx-126/677d3735607921b4d7503cf3?image=68f0430e2582886bb0066733&architecture=amd64&container-tabs=dockerfile
FROM registry.access.redhat.com/ubi10/nginx-126:10.0-1760573692

USER 0:0

# Update existing packages
RUN dnf upgrade -y --refresh --nodocs --setopt=install_weak_deps=0 \
  && dnf clean all

# Reset core NGINX permissions after updates, following RedHat's base image Dockerfile
RUN chown -R 1001:0 ${NGINX_CONF_PATH} && \
    chown -R 1001:0 ${NGINX_APP_ROOT}/etc && \
    chown -R 1001:0 ${NGINX_APP_ROOT}/src/nginx-start/  && \
    chown -R 1001:0 ${NGINX_CONTAINER_SCRIPTS_PATH}/nginx-start && \
    chown -R 1001:0 /var/lib/nginx /var/log/nginx /run && \
    chmod    ug+rw  ${NGINX_CONF_PATH} && \
    chmod -R ug+rwX ${NGINX_APP_ROOT}/etc && \
    chmod -R ug+rwX ${NGINX_APP_ROOT}/src/nginx-start/  && \
    chmod -R ug+rwX ${NGINX_CONTAINER_SCRIPTS_PATH}/nginx-start && \
    chmod -R ug+rwX /var/lib/nginx /var/log/nginx /run

USER default

# Add Dynatrace
COPY --from=dynatrace_source /opt/dynatrace/oneagent /opt/dynatrace/oneagent


# Add application sources
# The NGINX_CONF_PATH environment variable is set in the base image from Red Hat
# in the Dockerfile, where it's set to /etc/nginx/nginx.conf
COPY nginx.conf "${NGINX_CONF_PATH}"

# The nginx.conf file imports the mime.types file and expects it to be in the
# same directory as the nginx.conf file.
COPY mime.types /etc/nginx/
COPY dist ./public/

# Run script uses standard ways to run the application
CMD ["nginx", "-g", "daemon off;"]
