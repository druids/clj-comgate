clj-comgate
===========

A client for [Comgate Payments API](https://platebnibrana.comgate.cz/cz/protokol-api).

[![CircleCI](https://circleci.com/gh/druids/clj-comgate.svg?style=svg)](https://circleci.com/gh/druids/clj-comgate)
[![Dependencies Status](https://jarkeeper.com/druids/clj-comgate/status.png)](https://jarkeeper.com/druids/clj-comgate)
[![License](https://img.shields.io/badge/MIT-Clause-blue.svg)](https://opensource.org/licenses/MIT)


Leiningen/Boot
--------------

```clojure
[clj-comgate "0.1.0"]
```


Documentation
-------------

All functions are designed to return errors instead of throwing exceptions.

All API calls return a tuple within following structure: `[keyword response http-response]` where`keyword` can be:
- :ok when a response is a success and parsed
- :error when a response is parsed but it's an error response
- :error-unmarshalling when a response is not a well formated

A `response` is a parsed body of an original HTTP response.

To be able to run examples this line is needed:

```clojure
(require '[clj-comgate.core :as comgate])
```

### create-payment

Creates a new payment within `opts`.

```clojure
(comgate/create-payment {:merchant "asdf", :secret "qwerty", :price 10000, ...})
;; [:ok
;;  {:code 0, :message "OK", :redirect "https://payments.comgate.cz/...
;;  {:request-time 386, ...
```

To see all possible options see
[https://platebnibrana.comgate.cz/cz/protokol-api](https://platebnibrana.comgate.cz/cz/protokol-api)

Example of an error response:

```clojure
(comgate/create-payment {:merchant "asdf", :secret "qwerty", :price 10000, ...})
;; [:error
;;  {:code 1400, :message "Access from unauthorized location [127.0.0.1]!"
;;  {:request-time 386, ...
```

### get-status

Returns a status of requested payment by `opts`.

```clojure
(comgate/get-status {:merchant "asdf", :secret "qwerty", :transId "AAAA-BBBB-CCCC"})
;; [:ok
;;  {:code 0, :label "Test label", :message "OK", ...
;;  {:request-time 386, ...
```


### payment-result-response->map

In case you want to handle [the push payment result](https://platebnibrana.comgate.cz/cz/protokol-api#sidemenu-link-13)
you can use `payment-result-response->map` to parse a request body:

```clojure
(:body request) ;; a Ring request "merchant=merchant_com&test=false&price=10000...
(comgate/payment-result-response->map (:body request)) ;; {:merchant "merchant_com", ...
```

When accepting the push request Comgate expects OK response, thus you can use `comgate/payment-result-success-response`
for it (it's a Ring's response).
