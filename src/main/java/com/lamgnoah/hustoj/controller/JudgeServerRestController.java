package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.domain.pojos.JudgeServerStatus;
import com.lamgnoah.hustoj.dto.JudgeServerStatusDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1")
public class JudgeServerRestController {

  private final RedisTemplate redisTemplate;

  @Autowired
  public JudgeServerRestController(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostMapping(value = "/judge_server_heartbeat")
  public ResponseEntity<JudgeServerStatusDTO> handleHeartbeat(@RequestBody JudgeServerStatus judgeServerStatus) {
    redisTemplate.opsForValue().set("judge-server:status", judgeServerStatus);
    JudgeServerStatusDTO response = JudgeServerStatusDTO.builder().data("success").error(null)
        .build();
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/judge-server/status")
  public List<JudgeServerStatus> getStatus() {
    List<JudgeServerStatus> judgeServerStatusList = new ArrayList<>();
    judgeServerStatusList.add((JudgeServerStatus) redisTemplate.opsForValue().get("judge-server:status"));
    return judgeServerStatusList;
  }
}
