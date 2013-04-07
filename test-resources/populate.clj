(object/store :person :bruce_willis
              {:data  (json/clj->json {:firstName "Bruce"
                                       :lastName "Willis"})
               :as "application/json"
               :links (object/link-to
                       [:movie :die_hard_1 :movie]
                       [:movie :clones :movie])
               :metadata {:foo "foo-value"
                          :bar "bar-value"}})

(object/store :person :alan_rickman
              {:data (json/clj->json {:firstName "Alan"
                                      :lastName "Rickman"})
               :links (object/link-to
                       [:movie :die_hard_1 :movie])})

(object/store :person :alexander_godunov
       (json/clj->json {:firstName "Alexander"
                        :lastName "Godunov"})
       {:content-type "application/json"
        :links (object/create-links
                (object/link :movie :die_hard_1 :movie))})

(object/store :person :radha_mitchell
       (json/clj->json {:firstName "Radha"
                        :lastName "Mitchell"})
       {:content-type "application/json"
        :links (object/create-links
                (object/link :movie :clones :movie))})

(object/store :movie :die_hard_1
       (json/clj->json {:name "Piège de cristal"
                        :year 1988})
       {:content-type "application/json"
        :links (object/create-links
                (object/link :person :bruce_willis :actor)
                (object/link :person :alan_rickman :actor)
                (object/link :person :alexander_godunov :actor))})

(object/store :movie :clones
       (json/clj->json {:name "Clones"
                        :year 2009})
       {:content-type "application/json"
        :links (object/create-links
                (object/link :person :bruce_willis :actor))})
