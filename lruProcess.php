<?php
    function findLRU($time, $n)
    {
        $minimum = $time[0];
        $pos = 0;

        for($i = 1; $i < $n; $i++){
            if($time[$i] < $minimum){
                $minimum = $time[$i];
                $pos = $i;
            }
        }

        return $pos;
    }
    //int no_of_frames, no_of_pages, frames[10], pages[30], counter = 0, time[10], flag1, flag2, i, j, pos, faults = 0;
    if (isset($_POST['page-ref-string']) && isset($_POST['frame-size'])) {
        $pages = explode(" ", $_POST['page-ref-string']);
        $noOfPages = count($pages);
        
        $frameSize = $_POST['frame-size'];

        $frames = array_fill(0, $frameSize, -1);
        $time = array();
        $counter = 0;
        $faults = 0;
        
        $frameResults = array();

        for($i = 0; $i < $noOfPages; $i++) {
            $replaced = -3;
            $flag1 = $flag2 = 0;

            for($j = 0; $j < $frameSize; $j++) {
                if ($frames[$j] == $pages[$i]) {
                    $counter++;
                    $time[$j] == $counter;
                    $flag1 = $flag2 = 1;
                    break;
                }
            }

            if($flag1 == 0){ // replace a -1
                for($j = 0; $j < $frameSize; $j++){
                    if($frames[$j] == -1){
                        $counter++;
                        $faults++;
                        $frames[$j] = $pages[$i];
                        $replaced = -2;
                        $time[$j] = $counter;
                        $flag2 = 1;
                        break;
                    }
                }    
            }

            if($flag2 == 0){ // replace LRU
                $pos = findLRU($time, $frameSize);
                $counter++;
                $faults++;
                $replaced = $frames[$pos];
                $frames[$pos] = $pages[$i];
                $time[$pos] = $counter;
            }
            
            $frameResults[] = array(
                        "frameView"     => $frames,
                        "pageReplaced"  => $replaced,
                        "pageRequested" => $pages[$i]);
        }
        /* PRINT HERE */
        $out['frameResults'] = $frameResults;
        $out['pageFaults'] = $faults;
        $out['frameSize'] = $frameSize;
        echo json_encode($out);
    } else {
        echo "Something's wrong!";
    }
?>