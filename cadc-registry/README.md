# cadc-registry
client implementation of IVOA Registry specifications

## configuration

The RegistryClient and LocalAuthority classes in this library are configured
using a single configuration file: `cadc-registry.properties`.

Obsolete: The old `LocalAuthority.properties` file is no longer used.

### cadc-registry.properties
The cadc-registry.properties file specifies which local service is authoritative for various site-wide functions. The keys are IVOA standardID values representing the functions and the values are resourceID values (`ivo` scheme) for the service that implements that standard feature or a direct `https` 
base URL to the service. The latter is intended for specifying the registry service to use or for
services like an OpenID issuer that does not implement IVOA VOSI-capabilities.

All config in this file is now optional, including the _baseURL_. Failures should only occur if an actual lookup is attempted.

Example:
```
# configure RegistryClient
ca.nrc.cadc.reg.client.RegistryClient.baseURL =  https://ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg

# local IVOA CDP service
ivo://ivoa.net/std/CDP#delegate-1.0 = ivo://cadc.nrc.ca/cred
ivo://ivoa.net/std/CDP#proxy-1.0 = ivo://cadc.nrc.ca/cred

# local IVOA GMS service
ivo://ivoa.net/std/GMS#search-1.0 = ivo://cadc.nrc.ca/gms

# local CADC/CANFAR users service
ivo://ivoa.net/std/UMS#users-0.1 = ivo://cadc.nrc.ca/gms    
ivo://ivoa.net/std/UMS#login-0.1 = ivo://cadc.nrc.ca/gms           

## OIDC issuer
ivo://ivoa.net/sso#OpenID = https://oidc.example.net/
```
