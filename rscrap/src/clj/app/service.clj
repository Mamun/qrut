(ns app.service
  (:require
    [clojure.tools.logging :as log]
    [app.state :as s]
    [dadysql.jdbc :as j]))


(defn load-material-type []
  (hash-map
    "Instance_theDossierConditions_theMaterialInfo$0_mCode" {"612" "Telefon/Handy",
                                                             "616" "Computer",
                                                             "618" "Zubehör PC",
                                                             "610" "TV/HIFI Geräte",
                                                             "320" "Diverse Weiße Ware",
                                                             "0"   "Kartenantrag ohne Kauf",
                                                             "611" "Photo/Video",
                                                             "322" "Kühlschrank oder Gefrierschrank",
                                                             "323" "Spül-/Waschmaschine"},
    "Instance_theDossierConditions_theVendorInfo_mSalesmanId" {"mustermann" "Mustermann Max",
                                                               "redouan"    "redouan redouan",
                                                               "2182442"    "Standard Benutzer Standard Benutzer",
                                                               "otatli"     "Tatli Özgür",
                                                               "test"       "Test Test"}))





(comment

  (load-material-type)
  )