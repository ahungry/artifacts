(ns ahungry.art.entity.char
  (:require
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.bank :as bank]
   [ahungry.art.queue :as queue]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]))

(defn epoch []
  (.getEpochSecond (jt/instant)))

(defn to-epoch [instant]
  (.getEpochSecond (jt/instant instant)))

(defn get-count-items [m]
  (apply + (map :quantity m)))

;; Could use select-keys here but this is a nice visual
(defn filter-columns [{:keys [name level max_hp hp x y xp max_xp gold] :as m}]
  {:name name
   :level level
   :attack_fire (:attack_fire m)
   :attack_earth (:attack_earth m)
   :attack_water (:attack_water m)
   :attack_air (:attack_air m)
   :max_hp max_hp
   :hp hp
   :x x
   :y y
   :xp xp
   :max_xp max_xp
   :gold gold
   :woodcutting_level (:woodcutting_level m)
   :woodcutting_xp (:woodcutting_xp m)
   :woodcutting_max_xp (:woodcutting_max_xp m)
   :fishing_level (:fishing_level m)
   :fishing_xp (:fishing_xp m)
   :fishing_max_xp (:fishing_max_xp m)
   :alchemy_level (:alchemy_level m)
   :alchemy_xp (:alchemy_xp m)
   :alchemy_max_xp (:alchemy_max_xp m)
   :mining_level (:mining_level m)
   :mining_xp (:mining_xp m)
   :mining_max_xp (:mining_max_xp m)
   :weaponcrafting_level (:weaponcrafting_level m)
   :weaponcrafting_xp (:weaponcrafting_xp m)
   :weaponcrafting_max_xp (:weaponcrafting_max_xp m)
   :gearcrafting_level (:gearcrafting_level m)
   :gearcrafting_xp (:gearcrafting_xp m)
   :gearcrafting_max_xp (:gearcrafting_max_xp m)
   :jewelrycrafting_level (:jewelrycrafting_level m)
   :jewelrycrafting_xp (:jewelrycrafting_xp m)
   :jewelrycrafting_max_xp (:jewelrycrafting_max_xp m)
   :cooking_level (:cooking_level m)
   :cooking_xp (:cooking_xp m)
   :cooking_max_xp (:cooking_max_xp m)
   :inventory_max_items (:inventory_max_items m)
   :inventory_count_items (get-count-items (:inventory m))
   :weapon_slot (:weapon_slot m)
   :rune_slot (:rune_slot m)
   :shield_slot (:shield_slot m)
   :helmet_slot (:helmet_slot m)
   :body_armor_slot (:body_armor_slot m)
   :leg_armor_slot (:leg_armor_slot m)
   :boots_slot (:boots_slot m)
   :ring1_slot (:ring1_slot m)
   :ring2_slot (:ring2_slot m)
   :amulet_slot (:amulet_slot m)
   :artifact1_slot (:artifact1_slot m)
   :artifact2_slot (:artifact2_slot m)
   :artifact3_slot (:artifact3_slot m)
   :utility1_slot (:utility1_slot m)
   :utility2_slot (:utility2_slot m)
   :bag_slot (:bag_slot m)
   :cooldown_expiration (:cooldown_expiration m)
   :cooldown (:cooldown m)})

(defn progress [{:keys [x y name level xp max_xp hp max_hp gold
                        woodcutting_level woodcutting_xp woodcutting_max_xp
                        mining_level mining_xp mining_max_xp
                        gearcrafting_level gearcrafting_xp gearcrafting_max_xp
                        weaponcrafting_level weaponcrafting_xp weaponcrafting_max_xp
                        jewelrycrafting_level jewelrycrafting_xp jewelrycrafting_max_xp
                        ] :as m}]
  (log/info
   "Progress" name "lvl:" level ", xp:" xp "/" max_xp ", gold: " gold
   "wood lvl:" woodcutting_level ", xp: " woodcutting_xp "/" woodcutting_max_xp
   "mining lvl:" mining_level ", xp: " mining_xp "/" mining_max_xp
   "gearcrafting lvl:" gearcrafting_level ", xp: " gearcrafting_xp "/" gearcrafting_max_xp
   "weaponcrafting lvl:" weaponcrafting_level ", xp: " weaponcrafting_xp "/" weaponcrafting_max_xp
   "jewelrycrafting lvl:" jewelrycrafting_level ", xp: " jewelrycrafting_xp "/" jewelrycrafting_max_xp
   ", hp:" hp "/" max_hp)
  (log/info (emap/get-map x y)))

(defn get-char-from-db [name]
  (-> (j/query db ["select * from chars where name = ?" name]) first))

(defn import-char! [char]
  (let [data (filter-columns char)]
    (if (> (count (:name data)) 0)
      (do
        (if (get-char-from-db name)
          (j/update! db :chars data ["name=?" name])
          (j/insert! db :chars data))
        (when (> (count (:inventory char)) 0)
          (j/delete! db :inventory ["name=?" name])
          (j/insert-multi!
           db
           :inventory
           (map (fn [inv] (merge {:name (:name char)} inv)) (:inventory char ))))
        ;; (progress data)
        data)
      (log/error "Somehow an empty character import occurred..."))))

(defn fetch-char [name]
  (-> (sdk :get (str "/characters/" name)) :data))

(defn get-char [name]
  (or
   (get-char-from-db name)
   (import-char! (fetch-char name))))

(defn get-inventory [name]
  (j/query db ["select i.*, inv.quantity from inventory inv
left join items i on inv.code=i.code
where inv.name=? and inv.code <> ''" name]))

(defn get-inventory-consumable-count [name]
  (->> (get-inventory name)
       (filter #(= (:type %) "consumable"))
       (map :quantity)
       (apply +)))

(defn get-equippable-upgrades [name]
  (j/query db ["select i.*,
(
  select
    coalesce(case i.type

      when 'weapon' then (select ii.quality from chars c
        left join items ii on ii.code=c.weapon_slot where c.name = inv.name)

      when 'boots' then (select ii.quality from chars c
        left join items ii on ii.code=c.boots_slot where c.name = inv.name)

      when 'body_armor' then (select ii.quality from chars c
        left join items ii on ii.code=c.body_armor_slot where c.name = inv.name)

      when 'leg_armor' then (select ii.quality from chars c
        left join items ii on ii.code=c.leg_armor_slot where c.name = inv.name)

      when 'shield' then (select ii.quality from chars c
        left join items ii on ii.code=c.shield_slot where c.name = inv.name)

      when 'helmet' then (select ii.quality from chars c
        left join items ii on ii.code=c.helmet_slot where c.name = inv.name)

      when 'amulet' then (select ii.quality from chars c
        left join items ii on ii.code=c.amulet_slot where c.name = inv.name)

      when 'rune' then (select ii.quality from chars c
        left join items ii on ii.code=c.rune_slot where c.name = inv.name)

      when 'bag' then (select ii.quality from chars c
        left join items ii on ii.code=c.bag_slot where c.name = inv.name)

      when 'utility' then (select ii.quality from chars c
        left join items ii on ii.code=c.utility1_slot where c.name = inv.name)

      when 'ring' then (select ii.quality from chars c
        left join items ii on ii.code=c.ring1_slot where c.name = inv.name)

      when 'artifact' then (select ii.quality from chars c
        left join items ii on ii.code=c.artifact1_slot where c.name = inv.name)

      else -1
    end, -1)
) as existing_quality,
(
  select
    coalesce(case i.type

      when 'weapon' then (select ii.quality from chars c
        left join items ii on ii.code=c.weapon_slot where c.name = inv.name)

      when 'boots' then (select ii.quality from chars c
        left join items ii on ii.code=c.boots_slot where c.name = inv.name)

      when 'body_armor' then (select ii.quality from chars c
        left join items ii on ii.code=c.body_armor_slot where c.name = inv.name)

      when 'leg_armor' then (select ii.quality from chars c
        left join items ii on ii.code=c.leg_armor_slot where c.name = inv.name)

      when 'shield' then (select ii.quality from chars c
        left join items ii on ii.code=c.shield_slot where c.name = inv.name)

      when 'helmet' then (select ii.quality from chars c
        left join items ii on ii.code=c.helmet_slot where c.name = inv.name)

      when 'amulet' then (select ii.quality from chars c
        left join items ii on ii.code=c.amulet_slot where c.name = inv.name)

      when 'rune' then (select ii.quality from chars c
        left join items ii on ii.code=c.rune_slot where c.name = inv.name)

      when 'bag' then (select ii.quality from chars c
        left join items ii on ii.code=c.bag_slot where c.name = inv.name)

      when 'utility' then (select ii.quality from chars c
        left join items ii on ii.code=c.utility2_slot where c.name = inv.name)

      when 'ring' then (select ii.quality from chars c
        left join items ii on ii.code=c.ring2_slot where c.name = inv.name)

      when 'artifact' then (select ii.quality from chars c
        left join items ii on ii.code=c.artifact2_slot where c.name = inv.name)

      else -1
    end, -1)
) as existing_quality2,
(
  select
    case i.type

      when 'ring' then (
        select
          case when
            (select coalesce(iii.quality, 0) from chars iic
             left join items iii on (iii.code=iic.ring1_slot) where iic.name=inv.name)
            >
            (select coalesce(iiii.quality, 0) from chars iiic
             left join items iiii on (iiii.code=iiic.ring2_slot) where iiic.name=inv.name)
          then 'ring2' else 'ring1'
          end
      )

      when 'utility' then (
        select
          case when
            (select coalesce(iii.quality, 0) from chars iic
             left join items iii on (iii.code=iic.utility1_slot) where iic.name=inv.name)
            >
            (select coalesce(iiii.quality, 0) from chars iiic
             left join items iiii on (iiii.code=iiic.utility2_slot) where iiic.name=inv.name)
          then 'utility2' else 'utility1'
          end
      )

-- TODO: Need to account for artifact_3 as well...
      when 'artifact' then (
        select
          case when
            (select coalesce(iii.quality, 0) from chars iic
             left join items iii on (iii.code=iic.artifact1_slot) where iic.name=inv.name)
            >
            (select coalesce(iiii.quality, 0) from chars iiic
             left join items iiii on (iiii.code=iiic.artifact2_slot) where iiic.name=inv.name)
          then 'artifact2' else 'artifact1'
          end
      )

      else i.type
    end
) as slot_type
from inventory inv
left join items i on inv.code=i.code
where inv.name=? and inv.code <> ''
and i.type in ('weapon', 'boots', 'body_armor', 'leg_armor', 'utility',
'shield', 'helmet', 'ring', 'amulet', 'rune', 'bag', 'artifact')
and quality > min(existing_quality, existing_quality2)
order by type, quality desc" name]))

(defn get-delay [name]
  (- (to-epoch (:cooldown_expiration (get-char name)))
     (epoch)))

(defn get-hp [name]
  (select-keys (get-char name) [:hp :max_hp]))

;; Every action returns our character state, so we can follow the pattern here.
(defn do-action! [action name & [body]]
  (let [res ((sdk-for name) action body)]
    (if (:error res)
      (do
        (log/error "Action failed - fix the code..." name)
        ;; FIXME: brute force here...
        (log/warn "WIPING THE QUEUE - WE SHOULD NEVER BE HERE!!!")
        (queue/clear)
        ;; (System/exit 1)
        )
      (do
        ;; (log/info "Action success for:" action)
        ;; (log/info "Next delay pre-update: " (get-delay name))
        ;; (log/info "Cooldown data object:" (get-in res [:data :cooldown]))
        ;; (log/info "Cooldown data character:" (get-in res [:data :character :cooldown_expiration]))
        (import-char! (get-in res [:data :character]))
        ;; (log/info "Next delay in: " (get-delay name))
        ;; (log/info "Cooldown data character db:" (:cooldown_expiration (get-char name)))
        ))
    res))

;; 0 arity actions
(def do-rest! (partial do-action! :rest))
(def do-fight! (partial do-action! :fight))
(def do-gather! (partial do-action! :gathering))

(defn do-move! [{:keys [x y]} name]
  (do-action! :move name {:x x :y y}))

(defn do-unequip! [{:keys [code slot]} name]
  (do-action! :unequip name {:slot slot}))

(defn do-equip! [{:keys [code slot]} name]
  (do-action! :equip name {:code code :slot slot}))

(defn do-crafting! [{:keys [code]} name]
  (do-action! :crafting name {:code code}))

(defn do-recycling! [{:keys [code]} name]
  (do-action! :recycling name {:code code}))

(defn do-bank-deposit! [{:keys [code quantity]} name]
  (let [res (do-action! :bank-deposit name {:code code :quantity quantity})]
    (bank/import-bank!)
    res))

(defn do-bank-withdraw! [{:keys [code quantity]} name]
  (let [res (do-action! :bank-withdraw name {:code code :quantity quantity})]
    (bank/import-bank!)
    res))

(defn get-hunting-grounds [name]
  (let [char (get-char name)]
    (-> (emap/get-hunting-grounds
         {:hp (:max_hp char)
          :attack (+ (:attack_air char)
                     (:attack_earth char)
                     (:attack_fire char)
                     (:attack_water char))})
        first)))

(defn get-woodcutting-grounds [name]
  (let [char (get-char name)]
    (-> (emap/get-woodcutting-grounds
         {:woodcutting_level (:woodcutting_level char)})
        first)))

(defn full-health? [name]
  (let [char (get-hp name)]
    (= (:hp char) (:max_hp char))))

(defn can-act? [name]
  (> 0 (get-delay name)))

(defn is-close-to-encumbered? [name]
  (let [char (get-char name)]
    (>= (/ (:inventory_count_items char)
           (:inventory_max_items char)) 0.9)))

(defn is-encumbered? [name]
  (let [char (get-char name)]
    (>= (:inventory_count_items char)
        (:inventory_max_items char))))
