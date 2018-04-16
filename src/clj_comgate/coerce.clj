(ns clj-comgate.coerce)


(defmulti ->str
  type)


(defmethod ->str clojure.lang.Keyword
  [v]
  (name v))


(defmethod ->str :default
  [v]
  (str v))
