package task

import (
	"errors"
	"log"
	"time"
)

// ErrTimedOut is returned when an operation times out
var ErrTimedOut = errors.New("timed out performing task")

// DoRetryWithTimeout performs given task with given timeout and timeBeforeRetry.
// The function t returns a result out, a boolean to retry, and an error
// containing the reason for failure.
func DoRetryWithTimeout(t func() (interface{}, bool, error), timeout, timeBeforeRetry time.Duration) (interface{}, error) {
	done := make(chan bool, 1)
	quit := make(chan bool, 1)
	var out interface{}
	var err error
	var retry bool

	go func() {
		count := 0
		for {
			select {
			case q := <-quit:
				if q {
					return
				}

			default:
				out, retry, err = t()
				if err == nil || !retry {
					done <- true
					return
				}

				log.Printf("%v Next retry in: %v", err, timeBeforeRetry)
				time.Sleep(timeBeforeRetry)
			}

			count++
		}
	}()

	select {
	case <-done:
		return out, err
	case <-time.After(timeout):
		quit <- true
		return out, ErrTimedOut
	}
}
