(ns ahungry.art-test
  (:require [clojure.test :refer :all]
            [ahungry.art.routine.crafting :as rc]
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

(deftest test-get-based-on-lowest-skill
  (testing "We can choose what to do based on lowest skill to keep them even."
    (let [craftables [{:skill "woodcutting" :code "ash_plank"}
                      {:skill "weaponcrafting" :code "fire_staff"}
                      {:skill "weaponcrafting" :code "slime_dagger"}
                      {:skill "gearcrafting" :code "helmet"}]
          character {:woodcutting_level 3
                     :weaponcrafting_level 1
                     :gearcrafting_level 2}]
      (is (= "fire_staff" (:code (first (rc/sort-by-lowest-skill character craftables))))))))
