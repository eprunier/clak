(object/store :person :bruce_willis
       (json/clj->json {:firstName "Bruce"
                        :lastName "Willis"})
       {:content-type "application/json"
        :links (object/create-links
                (object/link :movie :die_hard_1 :movie)
                (object/link :movie :clones :movie))})

(object/store :person :alan_rickman
       (json/clj->json {:firstName "Alan"
                        :lastName "Rickman"})
       {:content-type "application/json"
        :links (object/create-links
                (object/link :movie :die_hard_1 :movie))})

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
