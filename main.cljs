(ns script
  (:require
   ["playwright$default" :as playwright]
   ["playwright$default" :refer [firefox]]
   ["argparse" :as argparse :refer [ArgumentParser]]
   [promesa.core :as p]))


(def parser (ArgumentParser. #js {:prog "main.cljs"
                                  :description "Example!"}))

(.add_argument parser "" #js {:help "url to take screenshot of"})
(.add_argument parser "-t" "--timeout" #js {:help "timeout before taking the screenshot"})

(.dir js/console (.parse_args parser (clj->js (vec *command-line-args*))))

(defn get-url-command-line-arg []
  (.- (.parse_args parser (clj->js (vec *command-line-args*)))))

(defn get-timeout-command-line-arg []
  (js/parseInt
   (or
    (.-t (.parse_args parser (clj->js (vec *command-line-args*))))
    (.-timeout (.parse_args parser (clj->js (vec *command-line-args*))))
    300)))

(defn sleep [f ms]
  (js/setTimeout f ms))

(defn sleep-promise+
  [obj wait]
  (js/Promise.
   (fn [resolve _reject]
     (sleep #(resolve obj) wait))))

(defn screenshot
  []
  (->
   (.launch firefox)
   (.then (fn [browser]
            (->
             (.newPage browser)
             (.then (fn [page]
                      (-> (.goto page (get-url-command-line-arg))
                          (.then #(sleep-promise+ browser (get-timeout-command-line-arg)))
                          (.then #(.screenshot page #js{:path "screenshot.png"}))
                          (.catch #(js/console.log %))
                          (.then #(.close browser))))))))))

(screenshot)