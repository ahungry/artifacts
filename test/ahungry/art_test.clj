(ns ahungry.art-test
  (:require [clojure.test :refer :all]
            [ahungry.art.entity.item :as i]))

(deftest test-get-weapon-quality
  (testing "We can calculate weapon qualities"
    (let [effects [{:code "attack_earth" :value 8}
                   {:code "critical_strike" :value 5}]
          quality (i/get-weapon-quality effects)]
      (is (> quality 8)))))

(deftest test-get-armor-quality
  (testing "We can calculate weapon qualities"
    (let [effects [{:code "hp" :value 40}
                   {:code "res_fire" :value 5}]
          quality (i/get-weapon-quality effects)]
      (is (> quality 40)))))
