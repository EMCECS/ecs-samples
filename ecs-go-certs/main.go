package main

import (
        "context"
	"crypto/tls"
	"flag"
        "fmt"
	"golang.org/x/crypto/acme/autocert"
	"log"
)

var (
     help = false
)

func parseFlags() {
	flag.BoolVar(&help, "help", false, "if true, prints usage")
	flag.Parse()
}

func printUsage() {
	log.Printf("----------------------------------------------")
	log.Printf(" Certgen: a command line utility to provide certificates")
	log.Printf(" ---------------------------------------------")
}

func main() {
	parseFlags()
	var m *autocert.Manager
        if  help {
            printUsage()
            return
        }

        hostPolicy := func (ctx context.Context, host string) error {
               allowedHost := "localhost" // please change name
               if host == allowedHost {
                     return nil
               }
               return fmt.Errorf("acme: only %s is allowed", allowedHost)
        }

	dataDir := "."
	m = &autocert.Manager {
		Prompt:     autocert.AcceptTOS,
		HostPolicy: hostPolicy,
		Cache:      autocert.DirCache(dataDir),
	}

	config := tls.Config{GetCertificate: m.GetCertificate}
	log.Printf("%vx", config)
}
