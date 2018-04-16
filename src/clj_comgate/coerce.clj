(ns clj-comgate.coerce)


(defmulti ->str
  (fn [v]
    (type v)))


(defmethod ->str clojure.lang.Keyword
  [v]
  (name v))


(defmethod ->str :default
  [v]
  (str v))
