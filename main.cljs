(ns script
  (:require
   ["playwright$default" :as playwright :refer [firefox]]
   ["argparse" :as argparse :refer [ArgumentParser]]))

(defn- sleep [f ms]
  (js/setTimeout f ms))

(def parser
  (ArgumentParser. #js {:prog "main.cljs"
                        :description "This is a simple screenshot app!"}))

(.add_argument parser ""
               #js {:help "url of which to take a screenshot"})
(.add_argument parser "-t" "--timeout"
               #js {:help "timeout after page visit before taking the screenshot"})

#_(.dir js/console (.parse_args parser (clj->js (vec *command-line-args*))))

(defn- get-url-command-line-arg
  "Parses the first string after calling the script

  `$ nbb main.cljs https://www.simon-neutert.de`"
  []
  (.- (.parse_args parser (clj->js (vec *command-line-args*)))))

(defn- get-timeout-command-line-arg
  "Parses the argument passed with `-t` or `--timeout` after calling the script

  `$ nbb main.cljs https://www.simon-neutert.de`"
  []
  (js/parseInt
   (or
    (.-t (.parse_args parser (clj->js (vec *command-line-args*))))
    (.-timeout (.parse_args parser (clj->js (vec *command-line-args*))))
    300)))

(defn sleep-promise+
  "wraps a Promise around a timeout returning the given object"
  [obj wait]
  (js/Promise.
   (fn [resolve _reject]
     (sleep #(resolve obj) wait))))

(defn url-without-protocol
  [url]
  (-> url
      (.replace "http://" "")
      (.replace "https://" "")))

(defn filename-screenshot
  [url]
  (prn url)
  (-> url
      (url-without-protocol)
      (str "_"  (str (js/Date.now)) ".png")))

(defn screenshot
  []
  (let [url (get-url-command-line-arg)]
    (->
     (.launch firefox)
     (.then (fn [browser]
              (->
               (.newPage browser)
               (.then (fn [page]
                        (-> (.goto page url)
                            (.then #(sleep-promise+ browser (get-timeout-command-line-arg)))
                            (.then #(.screenshot page #js{:path (filename-screenshot url)}))
                            (.catch #(js/console.log %))
                            (.then #(.close browser)))))))))))

(screenshot)