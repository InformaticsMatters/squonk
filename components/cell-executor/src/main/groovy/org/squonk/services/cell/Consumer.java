/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.services.cell;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Consume;
import org.apache.camel.Exchange;

/** Example consumer. Not used.
 * Created by timbo on 27/01/16.
 */
public class Consumer {


    @Consume(uri = "direct:inbound")
    public void consume(CamelContext context, Exchange exch, @Body String body) {
        System.out.println("Consumer " + context + " " + exch + " " + body);
    }



}
