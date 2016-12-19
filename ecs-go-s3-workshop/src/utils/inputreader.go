package utils

import (
	"bufio"
	"fmt"
	"os"
)

/*
 * Copyright 2016 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

// InputReader is a wrapper of bufio.Reader
type InputReader struct {
	*bufio.Reader
}

// GetInputStr returns input string
func (r *InputReader) GetInputStr(msg string) string {
	fmt.Println(msg)
	val, _ := r.ReadString('\n')
	return val[:len(val)-1]
}

// NewInputReader gets a new InputReader
func NewInputReader() *InputReader {
	return &InputReader{
		Reader: bufio.NewReader(os.Stdin),
	}
}
