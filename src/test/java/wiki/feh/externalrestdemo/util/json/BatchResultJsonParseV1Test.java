package wiki.feh.externalrestdemo.util.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BatchResultJsonParseV1Test {
    @InjectMocks
    private BatchResultJsonParseV1 batchResultJsonParseV1;

    @DisplayName("parseResultStringToApiResultList - 빈 리스트 반환 시 null 반환")
    @Test
    void parseResultStringToApiResultList_EmptyList() {
        // given
        String emptyListJson = "[]";

        // when
        var result = batchResultJsonParseV1.parseResultStringToApiResultList(emptyListJson);

        // then
        assertNull(result);
    }

    @DisplayName("parseResultStringToApiResultList - invalid JSON 시 null 반환")
    @Test
    void parseResultStringToApiResultList_InvalidJson() {
        // given
        String invalidJson = "{ this is not valid JSON }";

        // when
        var result = batchResultJsonParseV1.parseResultStringToApiResultList(invalidJson);

        // then
        assertNull(result);
    }

    @DisplayName("parseResultStringToApiResultList - ApiResult 파싱 불가능 시 null 반환")
    @Test
    void parseResultStringToApiResultList_InvalidApiResult() {
        // given
        String invalidApiResultJson = """
                [
                    {
                        "id": "rs_1234",
                        "type": "reasoning",
                        "summary": "this should be an array, not a string"
                    }
                ]
                """;

        // when
        var result = batchResultJsonParseV1.parseResultStringToApiResultList(invalidApiResultJson);

        // then
        assertNull(result);
    }

    @DisplayName("parseResultStringToApiResultList - null 입력 시 null 반환")
    @Test
    void parseResultStringToApiResultList_NullInput() {
        // when
        var result = batchResultJsonParseV1.parseResultStringToApiResultList(null);

        // then
        assertNull(result);
    }

    @DisplayName("parseResultStringToApiResultList - 정상 JSON 파싱 성공")
    @Test
    void parseResultStringToApiResultList_Success() {
        // given
        String validJson = """
                [
                    {
                        "key": "key_1",
                        "seq": 1,
                        "result": "result text 1"
                    },
                    {
                        "key": "key_2",
                        "seq": 2,
                        "result": "result text 2"
                    }
                ]
                """;

        // when
        var result = batchResultJsonParseV1.parseResultStringToApiResultList(validJson);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("key_1", result.getFirst().getKey());
        assertEquals(1, result.getFirst().getSeq());
        assertEquals("result text 1", result.getFirst().getResult());
    }


    @DisplayName("parseResponseJson - tree 파싱 불가능 시 null 반환")
    @Test
    void parseResponseJson_InvalidJson() {
        // given
        String invalidJson = "{ this is not valid JSON }";

        // when
        var result = batchResultJsonParseV1.parseResponseJson(invalidJson);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - custom_id 필드 누락 시 null 반환")
    @Test
    void parseResponseJson_MissingCustomId() {
        // given
        String jsonMissingCustomId = """
                {
                    "response": {
                        "body": {
                            "output": []
                        }
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonMissingCustomId);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - response 필드 누락 시 null 반환")
    @Test
    void parseResponseJson_MissingResponse() {
        // given
        String jsonMissingResponse = """
                {
                    "custom_id": "test_1",
                    "body": {
                        "output": []
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonMissingResponse);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - body 필드 누락 시 null 반환")
    @Test
    void parseResponseJson_MissingBody() {
        // given
        String jsonMissingBody = """
                {
                    "custom_id": "test_1",
                    "response": {
                        "output": []
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonMissingBody);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - output 필드 누락 시 null 반환")
    @Test
    void parseResponseJson_MissingOutput() {
        // given
        String jsonMissingOutput = """
                {
                    "custom_id": "test_1",
                    "response": {
                        "body": {
                        }
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonMissingOutput);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - output이 list가 아닌 경우 null 반환")
    @Test
    void parseResponseJson_OutputNotList() {
        // given
        String jsonOutputNotList = """
                {
                    "custom_id": "test_1",
                    "response": {
                        "body": {
                            "output": {}
                        }
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonOutputNotList);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - completed message output 항목 누락 시 null 반환")
    @Test
    void parseResponseJson_MissingCompletedMessageOutput() {
        // given
        String jsonMissingCompletedMessageOutput = """
                {
                    "custom_id": "test_1",
                    "response": {
                        "body": {
                            "output": [
                                {
                                    "type": "reasoning",
                                    "status": "completed"
                                }
                            ]
                        }
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonMissingCompletedMessageOutput);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - content의 text 필드 누락 시 null 반환")
    @Test
    void parseResponseJson_MissingTextInContent() {
        // given
        String jsonMissingTextInContent = """
                {
                    "custom_id": "test_1",
                    "response": {
                        "body": {
                            "output": [
                                {
                                    "type": "message",
                                    "status": "completed",
                                    "content": [
                                        {
                                            "type": "output_text"
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                }
                """;

        // when
        var result = batchResultJsonParseV1.parseResponseJson(jsonMissingTextInContent);

        // then
        assertNull(result);
    }

    @DisplayName("parseResponseJson - 정상 JSON 파싱")
    @Test
    void parseResponseJson_Success() {
        // given
        String validJson = """
                {
                   "id": "batch_req_1234",
                   "custom_id": "test_1",
                   "response": {
                     "status_code": 200,
                     "request_id": "1234",
                     "body": {
                       "id": "resp_689c706768fc819c9a154cee6d52038d0e185d7568179f85",
                       "object": "response",
                       "created_at": 1755082855,
                       "status": "completed",
                       "background": false,
                       "error": null,
                       "incomplete_details": null,
                       "instructions": [
                         {
                           "type": "message",
                           "content": [
                             {
                               "type": "input_text",
                               "text": "prompt text~"
                             }
                           ],
                           "role": "developer"
                         }
                       ],
                       "max_output_tokens": 4096,
                       "max_tool_calls": null,
                       "model": "gpt-5-mini-2025-08-07",
                       "output": [
                         {
                           "id": "rs_1234",
                           "type": "reasoning",
                           "summary": []
                         },
                         {
                           "id": "msg_1234",
                           "type": "message",
                           "status": "completed",
                           "content": [
                             {
                               "type": "output_text",
                               "annotations": [],
                               "logprobs": [],
                               "text": "response text"
                             }
                           ],
                           "role": "assistant"
                         }
                       ],
                       "parallel_tool_calls": true,
                       "previous_response_id": null,
                       "prompt": {
                         "id": "pmpt_1234",
                         "variables": {},
                         "version": "5"
                       },
                       "prompt_cache_key": null,
                       "reasoning": {
                         "effort": "medium",
                         "summary": null
                       },
                       "safety_identifier": null,
                       "service_tier": "default",
                       "store": true,
                       "temperature": 1,
                       "text": {
                         "format": {
                           "type": "text"
                         },
                         "verbosity": "medium"
                       },
                       "tool_choice": "auto",
                       "tools": [],
                       "top_logprobs": 0,
                       "top_p": 1,
                       "truncation": "disabled",
                       "usage": {
                         "input_tokens": 926,
                         "input_tokens_details": {
                           "cached_tokens": 0
                         },
                         "output_tokens": 3516,
                         "output_tokens_details": {
                           "reasoning_tokens": 3072
                         },
                         "total_tokens": 4442
                       },
                       "user": null,
                       "metadata": {}
                     }
                   },
                   "error": null
                 }
                """;

        String expectedHeroId = "test_1";
        String expectedResult = "response text";

        // when
        var result = batchResultJsonParseV1.parseResponseJson(validJson);

        // then
        assertNotNull(result);
        assertEquals(expectedHeroId, result.getT1());
        assertEquals(expectedResult, result.getT2());
    }
}