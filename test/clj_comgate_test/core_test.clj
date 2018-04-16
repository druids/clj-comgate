(ns clj-comgate-test.core-test
  (:require
    [clojure.test :refer [are deftest is testing]]
    [clj-comgate.core :as cg]
    [org.httpkit.fake :refer [with-fake-http]]))


(defn assert-response
  [expected-response expected-status [status response _]]
  (is (= expected-status status))
  (is (= expected-response response)))


(def host "http://localhost")


(deftest create-payment-test
  (testing "should not create payment"
    (let [body "code=1400&message=Access+from+unauthorized+location+%5B109.81.209.223%5D%21"
          expected-response {:code 1400, :message "Access from unauthorized location [109.81.209.223]!"}]
      (with-fake-http [(str cg/default-host "/v1.0/create") body]
        (assert-response expected-response :error (cg/create-payment {})))))

  (testing "should create payment"
    (let [params {:country :CZ
                  :curr :CZK
                  :email "my@mail.com"
                  :label "Test label"
                  :lang :cs
                  :merchant "0987"
                  :method :ALL
                  :phone nil
                  :prepareOnly true
                  :price 10000
                  :refId 1
                  :secret "1234"
                  :test true}
          expected-response {:code 0
                             :message "OK"
                             :redirect "https://payments.comgate.cz/client/instructions/index?id=AAAA-BBBB-CCCC"
                             :transId "AAAA-BBBB-CCCC"}
          body (str "code=0&message=OK&transId=AAAA-BBBB-CCCC&redirect=https%3A%2F%2Fpayments.comgate.cz%2F"
                    "client%2Finstructions%2Findex%3Fid%3DAAAA-BBBB-CCCC")]
      (with-fake-http [(str host "/v1.0/create") body]
        (assert-response expected-response :ok (cg/create-payment params host)))))

  (testing "should not create payment, invalid response"
    (let [params {:country :CZ
                  :curr :CZK
                  :email "my@mail.com"
                  :label "Test label"
                  :lang :cs
                  :merchant "0987"
                  :method :ALL
                  :phone nil
                  :prepareOnly true
                  :price 10000
                  :refId 1
                  :secret "1234"
                  :test true}]
      (with-fake-http [(str host "/v1.0/create") ""]
        (assert-response nil :error-unmarshalling (cg/create-payment params host))))))


(deftest get-status-test
  (testing "should return status pending"
    (let [params {:merchant "0987", :secret "1234", :transId "AAAA-BBBB-CCCC"}
          body (str "code=0&message=OK&merchant=0987&test=true&price=10000&curr=CZK&label=Test+label&refId=1"
                    "&cat=DIGITAL&method=&email=my%2Bcomgate%40gmail.com&name=&transId=AAAA-BBBB-CCCC"
                    "&secret=1234&status=PENDING&fee=unknown&vs=")
          expected-response {:cat :DIGITAL
                             :code 0
                             :curr :CZK
                             :email "my+comgate@gmail.com"
                             :fee "unknown"
                             :label "Test label"
                             :merchant "0987"
                             :message "OK"
                             :method nil
                             :name ""
                             :price 10000
                             :refId "1"
                             :secret "1234"
                             :status :PENDING
                             :test true
                             :transId "AAAA-BBBB-CCCC"
                             :vs ""}]
      (with-fake-http [(str cg/default-host "/v1.0/status") body]
        (assert-response expected-response :ok (cg/get-status params)))))

  (testing "should return status paid"
    (let [params {:merchant "0987", :secret "1234", :transId "AAAA-BBBB-CCCC"}
          body (str "code=0&message=OK&merchant=0987&test=true&price=10000&curr=CZK&label=Test+label&refId=1"
                    "&cat=DIGITAL&method=BANK_CZ_CSOB&email=my%2Bcomgate%40gmail.com&name=&transId=AAAA-BBBB-CCCC"
                    "&secret=1234&status=PAID&fee=unknown&vs=43279543")
          expected-response {:cat :DIGITAL
                             :code 0
                             :curr :CZK
                             :email "my+comgate@gmail.com"
                             :fee "unknown"
                             :label "Test label"
                             :merchant "0987"
                             :message "OK"
                             :method :BANK_CZ_CSOB
                             :name ""
                             :price 10000
                             :refId "1"
                             :secret "1234"
                             :status :PAID
                             :test true
                             :transId "AAAA-BBBB-CCCC"
                             :vs "43279543"}]
      (with-fake-http [(str cg/default-host "/v1.0/status") body]
        (assert-response expected-response :ok (cg/get-status params)))))

  (testing "should not return status"
    (let [body "code=1400&message=Access+from+unauthorized+location+%5B109.81.209.223%5D%21"
          expected-response {:code 1400, :message "Access from unauthorized location [109.81.209.223]!"}]
      (with-fake-http [(str cg/default-host "/v1.0/status") body]
        (assert-response expected-response :error (cg/get-status {})))))

  (testing "should not return status, invalid response"
    (let [params {:country :CZ
                  :curr :CZK
                  :email "my@mail.com"
                  :label "Test label"
                  :lang :cs
                  :merchant "0987"
                  :method :ALL
                  :phone nil
                  :prepareOnly true
                  :price 10000
                  :refId 1
                  :secret "1234"
                  :test true}]
      (with-fake-http [(str host "/v1.0/status") ""]
        (assert-response nil :error-unmarshalling (cg/get-status params host))))))
