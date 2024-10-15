
/** Definition url */
var CLIENT_URL = '/search/openapi/client/v7.0/index.html'
var ADMIN_URL = '/search/openapi/admin/v7.0/index.html'
var EXTS_URL = '/search/openapi/exts/v7.0/index.html'

/** Clients url */	
var CLIENT_DEFINITION_URL = "/search/api/client/v7.0/embedded/files/client/v7.0/openapi.yaml"
var ADMIN_DEFINITION_URL = "/search/api/client/v7.0/embedded/files/admin/v7.0/openapi.yaml"
var EXTS_DEFINITION_URL = "/search/api/client/v7.0/embedded/files/exts/v7.0/openapi.yaml"

var TOKEN = null;

function login(redirectPostfix) {
  const redirectUri = window.location.origin + redirectPostfix;
  const authorizationUrl = `/search/api/client/v7.0/user/auth/login?redirect_uri=${encodeURIComponent(redirectUri)}`;
  window.location.href = authorizationUrl;
}

function logout(redirectPostfix) {
  const redirectUri = window.location.origin + redirectPostfix;
  const authorizationUrl = `/search/api/client/v7.0/user/auth/logout?redirect_uri=${encodeURIComponent(redirectUri)}`;
  window.location.href = authorizationUrl;
}
