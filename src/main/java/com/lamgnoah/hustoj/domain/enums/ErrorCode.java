package com.lamgnoah.hustoj.domain.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {
  INTERNAL_SERVER_ERROR("000" , "Internal server error" , HttpStatus.INTERNAL_SERVER_ERROR),
  PROBLEM_NOTFOUND("001","Problem not found" , HttpStatus.NOT_FOUND),
  PROBLEM_EXISTS_CODE_ALREADY( "002" , "Problem code exists already" , HttpStatus.BAD_REQUEST),
  PROBLEM_EXISTS_TITLE_ALREADY("003" , "Problem title exists already" , HttpStatus.BAD_REQUEST ),
  SAMPLE_IO_INVALID("004", "Sample input/output is not valid", HttpStatus.BAD_REQUEST ),
  TEST_DATA_PATH_INVALID( "005", "Test data path is invalid"  , HttpStatus.BAD_REQUEST),
  EMPTY_FILE("006", "File upload is empty" , HttpStatus.BAD_REQUEST ),
  EMPTY_ZIP_FILE("007", "Empty Zip file", HttpStatus.BAD_REQUEST),
  JUDGE_POST_ERROR("009" , "Failed to send code to judger module. Please check again" , HttpStatus.INTERNAL_SERVER_ERROR),
  NO_SUCH_SUBMISSION("010" , "Submission not found" , HttpStatus.NOT_FOUND),
  NO_SUCH_USER("011" , "User not found" , HttpStatus.NOT_FOUND ) ,
  HAVE_SAME_NAME_CONTEST("012" , "Contest already exists" , HttpStatus.BAD_REQUEST),
  START_TIME_IS_EARLY_THAN_NOW("013", "Start time is early than now", HttpStatus.BAD_REQUEST),
  START_TIME_IS_AFTER_THAN_END_TIME("014", "Start time is after than end time" , HttpStatus.BAD_REQUEST),
  NO_PASS_PROVIDED( "015", "Need password to enter contest" , HttpStatus.BAD_REQUEST ),
  NO_SUCH_CONTEST( "016", "Contest not found " , HttpStatus.NOT_FOUND ),
  CONTEST_IS_ENDED("017", "Contest ended", HttpStatus.BAD_REQUEST),
  DUPLICATED_USERNAME("018", "User with username is exists", HttpStatus.BAD_REQUEST),
  DUPLICATED_EMAIL("019", "User with email is exists" , HttpStatus.BAD_REQUEST ),
  USER_DISABLED( "020", "User is disabled" , HttpStatus.BAD_REQUEST),
  BAD_CREDENTIALS("021", "Bad credentials" , HttpStatus.BAD_REQUEST),
  NOT_A_ZIP_FILE("022", "Not a zip file",HttpStatus.BAD_REQUEST),
  INVALID_TEST_DATA_FORMAT("023", "Test data input/output wrong format" ,HttpStatus.BAD_REQUEST),
  NO_SUCH_PROBLEM("024", "No such problem", HttpStatus.BAD_REQUEST ),
  CONTEST_NOT_GOING("025", "Contest not started",  HttpStatus.BAD_REQUEST),
  CAN_ONLY_CHANGE_FROM_NOT_STARTED("026", "Contest can only changed when not started", HttpStatus.BAD_REQUEST),
  BAD_CONTEST_STATUS("027", "Bad contest status", HttpStatus.BAD_REQUEST),
  CONTEST_PROBLEM_EXISTS_CODE_ALREADY( "028", "Contest problem code already exists" , HttpStatus.BAD_REQUEST ),
  CONTEST_PROBLEM_EXISTS_TITLE_ALREADY( "023", "Contest problem title already exists" , HttpStatus.BAD_REQUEST ),
  PROBLEM_ALREADY_IN_CONTEST( "030" , "Problem already in contest" , HttpStatus.BAD_REQUEST),
  NO_SUCH_PROBLEM_IN_CONTEST("031","No such problem in contest " ,HttpStatus.BAD_REQUEST),
  CANNOT_UPDATE_PROBLEM_FROM_PUBLIC( "032" ,"Cannot update problem from contest " ,HttpStatus.BAD_REQUEST ),
  CANNOT_UPDATE_PROBLEM_WHEN_CONTEST_ENDED( "033" ,"Cannot update problem when contest is ended" ,HttpStatus.BAD_REQUEST),
  NOT_PASS_CONTEST_USER("034", "Password required" , HttpStatus.BAD_REQUEST),
  BAD_PASSWORD( "035", "Wrong password" , HttpStatus.BAD_REQUEST),
  USER_ALREADY_IN_CONTEST( "036", "User already in contest" , HttpStatus.BAD_REQUEST),
  PROCESS_TESTCASE_ERROR("037", "Process test case error" , HttpStatus.BAD_REQUEST),
  WRONG_PROBLEM_RULE_TYPE("038" , "Wrong problem rule type" , HttpStatus.BAD_REQUEST),
  PROBLEM_REFERENCED( "039", "Problem already in some contest , cannot delete " ,HttpStatus.BAD_REQUEST ),
  OBJECT_NOT_CREATED_BY_USER( "040","Object not created by user " ,HttpStatus.BAD_REQUEST ),
  NOT_PUBLIC_CONTEST_USER( "041", "Need to join to this contest by submit 1 problem" ,HttpStatus.BAD_REQUEST ),
  TEST_CASE_NOT_FOUND("042", "Test case not found " , HttpStatus.BAD_REQUEST),
  CANNOT_DELETE_CONTEST_PROBLEM_HAS_SUBMISSION("043" , "Cannot delete problem as it has submission" , HttpStatus.BAD_REQUEST)
  ;


  private final String code;
  private final String message;
  private final HttpStatus status;
  ErrorCode(String code ,String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }
}
