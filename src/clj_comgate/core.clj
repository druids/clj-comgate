(ns clj-comgate.core
  (:require
    [clojure.string :as string]
    [clj-comgate.coerce :refer [->str]]
    [org.httpkit.client :as http]
    [schema.coerce :as c]
    [schema.core :as s]
    [schema.utils :as u]
    [tol.core :as tol])
  (:import
    [java.net URLDecoder]))


(defn- url-decode
  [s]
  (URLDecoder/decode s "utf8"))


(defn- split-param
  [param]
  (-> (string/split param #"=")
      (concat (repeat ""))
      (->> (take 2))))


(defn- update-kv
  [fun m]
  (reduce-kv (fn [acc k v]
               (assoc acc (fun k) (fun v)))
             {}
             m))


(defn- update-key-if-exists
  [m k f]
  (if (contains? m k)
    (update m k f)
    m))


(defn- keyword-or-nil
  [v]
  (when-not (-> v name string/blank?)
    v))


(def PaymentSuccessResponse
  {:code s/Int
   :message s/Str
   (s/optional-key :transId) s/Str
   (s/optional-key :redirect) s/Str})

(def ErrorResponse
  {:code s/Int
   :message s/Str})

(def StatusSuccessResponse
  {:code s/Int
   :message s/Str
   :merchant s/Str
   :test s/Bool
   :price s/Int
   :curr s/Keyword
   :label s/Str
   :refId s/Str
   (s/optional-key :payerId) s/Str
   (s/optional-key :method) s/Keyword
   (s/optional-key :account) s/Str
   :email s/Str
   (s/optional-key :phone) s/Str
   (s/optional-key :name) s/Str
   :transId s/Str
   :secret s/Str
   :status s/Keyword
   (s/optional-key :fee) s/Str
   (s/optional-key :eetData) s/Str
   (s/optional-key :cat) s/Keyword
   (s/optional-key :vs) s/Str})


(def PaymentResultResponse
  {:merchant s/Str
   :test s/Bool
   :price s/Int
   :curr s/Keyword
   :label s/Str
   :refId s/Str
   (s/optional-key :payerId) s/Str
   (s/optional-key :payerName) s/Str
   (s/optional-key :payerAcc) s/Str
   (s/optional-key :method) s/Keyword
   (s/optional-key :account) s/Str
   :email s/Str
   (s/optional-key :phone) s/Str
   (s/optional-key :name) s/Str
   :transId s/Str
   :secret s/Str
   :status s/Keyword
   (s/optional-key :fee) s/Str
   (s/optional-key :eetData) s/Str
   (s/optional-key :cat) s/Keyword
   (s/optional-key :vs) s/Str})


(defn coerce-response
  [success-c error-c response-m]
  (let [coerce (if (or (nil? error-c)
                       (= "0" (:code response-m)))
                 success-c
                 error-c)]
    (-> response-m
        coerce
        (update-key-if-exists :method keyword-or-nil)))) ;; otherwise keyword is created for an empty string


(defn query->map
  [coerce-success-response coerce-error-response qstr]
  (when-not (string/blank? qstr)
    (some->> (string/split qstr #"&")
      seq
      (mapcat split-param)
      (map url-decode)
      (partition 2)
      (map vec)
      (into {})
      (tol/update-keys keyword)
      (coerce-response coerce-success-response coerce-error-response))))


;; coercers for responses
(def coerce-payment-success-response (c/coercer PaymentSuccessResponse c/string-coercion-matcher))
(def coerce-status-success-response (c/coercer StatusSuccessResponse c/string-coercion-matcher))
(def coerce-error-response (c/coercer ErrorResponse c/string-coercion-matcher))
(def coerce-payment-result-response (c/coercer PaymentResultResponse c/string-coercion-matcher))


;; parsers for responses
(def payment-success-response->map (partial query->map coerce-payment-success-response coerce-error-response))
(def status-success-response->map (partial query->map coerce-status-success-response coerce-error-response))
(def payment-result-response->map (partial query->map coerce-payment-result-response nil))

(def payment-result-success-response
  {:status 200
   :body "code=0&message=OK"
   :headers {"Content-Type" "application/x-www-form-urlencoded; charset=utf-8"}})


(defn- http-opts
  [params]
  {:as :text
   :headers {"Content-Type" "application/x-www-form-urlencoded"}
   :query-params (update-kv ->str params)})


(def default-host "https://payments.comgate.cz")


(defn create-payment
  "Creates a new payment within `opts`.

  It returns a tuple like `[keyword response http-response]`
   A `keyword` can be:
   - :ok when a response is a success and parsed
   - :error when a response is parsed but it's an error response
   - :error-unmarshalling when a response is not a well formatted"
  ([opts]
   (create-payment opts default-host))
  ([opts host]
   (let [uri (str host "/v1.0/create")
         http-response @(http/post uri (http-opts opts))
         response (payment-success-response->map (:body http-response))]
     (case (:code response)
       0 [:ok response http-response]
       nil [:error-unmarshalling response http-response]
       [:error response http-response]))))


(defn get-status
  "Returns a status of requested payment by `opts`.

  It returns a tuple like `[keyword response http-response]`
   A `keyword` can be:
   - :ok when a response is a success and parsed
   - :error when a response is parsed but it's an error response
   - :error-unmarshalling when a response is not a well formatted"
  ([opts]
   (get-status opts default-host))
  ([opts host]
   (let [uri (str host "/v1.0/status")
         http-response @(http/post uri (http-opts opts))
         response (status-success-response->map (:body http-response))]
     (case (:code response)
       0 [:ok response http-response]
       nil [:error-unmarshalling response http-response]
       [:error response http-response]))))
