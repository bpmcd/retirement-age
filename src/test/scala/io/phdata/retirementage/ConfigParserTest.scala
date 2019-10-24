/*
 * Copyright 2018 phData Inc.
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

package io.phdata.retirementage

import io.phdata.retirementage.domain._
import org.scalatest.FunSuite

class ConfigParserTest extends FunSuite {
  test("parse config") {
    val testFile =
      """
        |kudu_masters:
        |  - master1
        |  - master2
        |databases:
        |  - name: database1
        |    tables:
        |      - name: parquet1
        |        storage_type: parquet
        |        expiration_column: col1
        |        expiration_days: 10
        |      - name: kudu1
        |        storage_type: kudu
        |        expiration_column: col1
        |        expiration_days: 100
        |        hold:
        |          active: true
        |          reason: "legal"
        |          owner: tony@phdata.io
        |        date_format_string: yyyy-MM-dd
        |        child_tables:
        |          - name: parquet2
        |            storage_type: parquet
        |            join_on:
        |              - parent: col1
        |                self: col1
        |              - parent: col2
        |                self: col2
        |            child_tables:
        |              - name: parquet2
        |                storage_type: parquet
        |                join_on:
        |                  - parent: col1
        |                    self: col1
        |      - name: customTable1
        |        storage_type: parquet
        |        filters:
        |          - filter: 'id = 1'
        |        hold:
        |          active: true
        |          reason: "legal"
        |          owner: random@random.io
      """.stripMargin

    val actual = RetirementConfigParser.parse(testFile)

    val expected =
      Config(
        Some(List("master1", "master2")),
        List(
          Database(
            "database1",
            List(
              DatedTable("parquet1", "parquet", "col1", 10, None, None, None),
              DatedTable(
                "kudu1",
                "kudu",
                "col1",
                100,
                Some(Hold(true, "legal", "tony@phdata.io")),
                Some("yyyy-MM-dd"),
                Some(
                  List(
                    ChildTable(
                      "parquet2",
                      "parquet",
                      List(JoinOn("col1", "col1"), JoinOn("col2", "col2")),
                      None,
                      Some(
                        List(
                          ChildTable("parquet2",
                                     "parquet",
                                     List(JoinOn("col1", "col1")),
                                     None,
                                     None)))
                    )
                  )
                )
              ),
              CustomTable("customTable1",
                          "parquet",
                          List(CustomFilter("id = 1")),
                          Some(Hold(true, "legal", "random@random.io")),
                          None)
            )
          ))
      )

    assertResult(expected)(actual)
  }

  test("parse test config") {
    val testFile =
      """
        |kudu_masters:
        |  - master1
        |  - master2
        |databases:
        |  - name: impala::pcb_rawsbx1
        |    tables:
        |      - name: rctcdp
        |        storage_type: kudu
        |        expiration_column: "dqc2dt"
        |        expiration_days: 100
        |        date_format_string: yyyyMM
        |        child_tables:
        |          - name: rcpdtp
        |            storage_type: kudu
        |            join_on:
        |              - parent: dqp2nb
        |                self: e0p2nb
        |              - parent: dqoinb
        |                self: e0oinb
      """.stripMargin

    val actual = RetirementConfigParser.parse(testFile)

    val expected =
      Config(
        Some(List("master1", "master2")),
        List(
          Database(
            "impala::pcb_rawsbx1",
            List(
              DatedTable(
                "rctcdp",
                "kudu",
                "dqc2dt",
                100,
                None,
                Some("yyyyMM"),
                Some(
                  List(
                    ChildTable(
                      "rcpdtp",
                      "kudu",
                      List(JoinOn("dqp2nb", "e0p2nb"), JoinOn("dqoinb", "e0oinb")),
                      None,
                      None
                    )
                  )
                )
              )
            )
          ))
      )

    assertResult(expected)(actual)
  }

}
